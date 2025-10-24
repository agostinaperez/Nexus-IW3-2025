package edu.iua.nexus.integration.cli1.model.business.impl;

import edu.iua.nexus.integration.cli1.model.DriverCli1;
import edu.iua.nexus.integration.cli1.model.business.interfaces.IDriverCli1Business;
import edu.iua.nexus.integration.cli1.model.repository.DriverCli1Repository;
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
public class DriverCli1Business implements IDriverCli1Business {

    @Autowired
    private DriverCli1Repository driverDAO;

    @Override
    public DriverCli1 addExternal(DriverCli1 driver) throws FoundException, BusinessException, NotFoundException {
        Optional<DriverCli1> foundDriver;

        // busco si existe y si esta lo actualizo
        foundDriver = driverDAO.findOneByIdCli1(driver.getIdCli1());
        if (foundDriver.isPresent()) {
            return updateDriverData(foundDriver.get(), driver, false);
        }

        //busco duplicado por dni
        foundDriver = driverDAO.findByDocument(driver.getDocument());
        if (foundDriver.isPresent()) {
            throw FoundException.builder()
                .message("Ya existe un conductor con DNI: " + driver.getDocument())
                .build();
        }

        // Creo nuevo conductor
        try {
            return driverDAO.save(driver);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    private DriverCli1 updateDriverData(DriverCli1 existingDriver, DriverCli1 newDriver, boolean updated) {
        if (!Objects.equals(existingDriver.getName(), newDriver.getName())) {
            existingDriver.setName(newDriver.getName());
            updated = true;
        }
        if (!Objects.equals(existingDriver.getLastName(), newDriver.getLastName())) {
            existingDriver.setLastName(newDriver.getLastName());
            updated = true;
        }
        return updated ? driverDAO.save(existingDriver) : existingDriver;
    }


}
