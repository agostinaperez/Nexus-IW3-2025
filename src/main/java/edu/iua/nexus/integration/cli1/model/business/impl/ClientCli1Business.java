package edu.iua.nexus.integration.cli1.model.business.impl;

import edu.iua.nexus.integration.cli1.model.ClientCli1;
import edu.iua.nexus.integration.cli1.model.business.interfaces.IClientCli1Business;
import edu.iua.nexus.integration.cli1.model.repository.ClientCli1Repository;
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
public class ClientCli1Business implements IClientCli1Business {

    @Autowired
    private ClientCli1Repository clientDAO;

    @Override
    public ClientCli1 addExternal(ClientCli1 client) throws BusinessException, NotFoundException, FoundException {
        Optional<ClientCli1> foundClient;
        //si lo encuentro por un id externo, lo actualizo
        foundClient = clientDAO.findOneByIdCli1(client.getIdCli1());
        if (foundClient.isPresent()) {
            return updateClientData(foundClient.get(), client, false);
        }

        // Si no existe, verifico q no haya otro con el mismo nombre (para evitar duplicados por nombre)
        foundClient = clientDAO.findByName(client.getName());
        if (foundClient.isPresent()) {
            throw FoundException.builder()
                .message("Ya existe un cliente con razón social: " + client.getName())
                .build();
        }

        // si no existe y su nombre no esta repetido, se agrega
        try {
            return clientDAO.save(client);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    // Funcion auxiliar para actualizar datos del client si han cambiado
    private ClientCli1 updateClientData(ClientCli1 foundClient, ClientCli1 newClient, boolean updated) {
        if (!Objects.equals(foundClient.getName(), newClient.getName())) {
            foundClient.setName(newClient.getName());
            updated = true;
        }
        if (!Objects.equals(foundClient.getEmail(), newClient.getEmail())) {
            foundClient.setEmail(newClient.getEmail());
            updated = true;
        }
        return updated ? clientDAO.save(foundClient) : foundClient;
    }
}