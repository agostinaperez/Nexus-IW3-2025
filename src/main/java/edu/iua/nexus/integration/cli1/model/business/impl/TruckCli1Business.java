package edu.iua.nexus.integration.cli1.model.business.impl;

import edu.iua.nexus.integration.cli1.model.TruckCli1;
import edu.iua.nexus.integration.cli1.model.business.interfaces.ITruckCli1Business;
import edu.iua.nexus.integration.cli1.model.repository.TruckCli1Repository;
import edu.iua.nexus.model.Tank;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class TruckCli1Business implements ITruckCli1Business {

    @Autowired
    private TruckCli1Repository truckDAO;

    @Override
    public TruckCli1 load(String idCli1) throws NotFoundException, BusinessException {
        Optional<TruckCli1> r;
        try {
            r = truckDAO.findOneByIdCli1(idCli1);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if (r.isEmpty()) {
            throw NotFoundException.builder().message("No se encuentra el camion idCli1=" + idCli1).build();
        }
        return r.get();
    }

    @Override
    public TruckCli1 addExternal(TruckCli1 truck) throws FoundException, BusinessException, NotFoundException {
        Optional<TruckCli1> foundTruck;

        //Busco x idcli1 y si no me fijo si hay duplicados x patente
        foundTruck = truckDAO.findOneByIdCli1(truck.getIdCli1());
        if (foundTruck.isPresent()) {
            return updateTruckData(foundTruck.get(), truck, false);
        }

        foundTruck = truckDAO.findByLicensePlate(truck.getLicensePlate());
        if (foundTruck.isPresent()) {
            throw FoundException.builder()
                .message("Ya existe un camión con patente: " + truck.getLicensePlate())
                .build();
        }

        // si no creo uno nuevo c sus tanques
        try {
            for (Tank tank : truck.getTanks()) {
                tank.setTruck(truck);
            }
            truckDAO.save(truck);
            return load(truck.getIdCli1());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    private TruckCli1 updateTruckData(TruckCli1 existingTruck, TruckCli1 newTruck, boolean updated) {
        if (!Objects.equals(existingTruck.getLicensePlate(), newTruck.getLicensePlate())) {
            existingTruck.setLicensePlate(newTruck.getLicensePlate());
            updated = true;
        }
        if (!Objects.equals(existingTruck.getDescription(), newTruck.getDescription())) {
            existingTruck.setDescription(newTruck.getDescription());
            updated = true;
        }

        // Comparar tanques
        if (!existingTruck.getTanks().equals(newTruck.getTanks())) {
            existingTruck.getTanks().clear();
            truckDAO.save(existingTruck);
            for (Tank tank : newTruck.getTanks()) {
                tank.setTruck(existingTruck);
                existingTruck.getTanks().add(tank);
            }
            updated = true;
        }

        return updated ? truckDAO.save(existingTruck) : existingTruck;
    }

}

