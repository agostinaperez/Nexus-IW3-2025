package edu.iua.nexus.integration.cli1.model.business.interfaces;

import edu.iua.nexus.integration.cli1.model.TruckCli1;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;


public interface ITruckCli1Business {

    public TruckCli1 load(String idCli1) throws NotFoundException, BusinessException;

    public TruckCli1 addExternal(TruckCli1 truck) throws FoundException, BusinessException, NotFoundException;
}
