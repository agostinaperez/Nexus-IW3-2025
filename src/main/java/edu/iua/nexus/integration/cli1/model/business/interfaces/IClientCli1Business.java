package edu.iua.nexus.integration.cli1.model.business.interfaces;

import edu.iua.nexus.integration.cli1.model.ClientCli1;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;

public interface IClientCli1Business {

    public ClientCli1 addExternal(ClientCli1 client) throws BusinessException, NotFoundException, FoundException;
}