package edu.iua.nexus.integration.cli3.model.business.interfaces;

import edu.iua.nexus.model.Detail;
import edu.iua.nexus.model.Order;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.ConflictException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;

public interface IOrderCli3Business {
    public Order validatePassword(int password) throws NotFoundException, BusinessException, ConflictException;

    public Order receiveDetails(Detail detail) throws NotFoundException, BusinessException, FoundException, ConflictException;

    //Punto 4
    public Order closeOrder(Long orderId) throws BusinessException, NotFoundException, ConflictException;
}
