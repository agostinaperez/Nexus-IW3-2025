package edu.iua.nexus.integration.cli3.model.business.interfaces;

import edu.iua.nexus.model.Detail;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;

public interface IDetailCli3Business {

    void add(Detail detail) throws FoundException, BusinessException, NotFoundException;
}
