package edu.iua.nexus.integration.cli2.model.business.interfaces;

import edu.iua.nexus.model.Order;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.ConflictException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;


public interface IOrderCli2Business {

    Order registerInitialWeighing(String orderNumber, float initialWeight) throws BusinessException, NotFoundException, FoundException, ConflictException;

    //ENDPOINT 5.0
    byte[] registerFinalWeighing(String orderNumber, float finalWeight) throws BusinessException, NotFoundException, FoundException, ConflictException;

    //ENDPOINT 5.1
    byte[] getConciliation(String licensePlate) throws BusinessException, NotFoundException;
}
