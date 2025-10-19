package edu.iua.nexus.integration.cli1.model.business.interfaces;

import edu.iua.nexus.integration.cli1.model.DriverCli1;
import edu.iua.nexus.model.Driver;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;

public interface IDriverCli1Business {

    public Driver addExternal(DriverCli1 driver) throws FoundException, BusinessException, NotFoundException;

}
