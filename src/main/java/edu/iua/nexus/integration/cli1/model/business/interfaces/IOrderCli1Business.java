package edu.iua.nexus.integration.cli1.model.business.interfaces;


import java.util.List;

import edu.iua.nexus.integration.cli1.model.OrderCli1;
import edu.iua.nexus.model.business.BadRequestException;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;


public interface IOrderCli1Business {

    public OrderCli1 load(String orderNumberCli1) throws NotFoundException, BusinessException;

    public List<OrderCli1> list() throws BusinessException;

    public OrderCli1 add(OrderCli1 order) throws FoundException, BusinessException;

    public OrderCli1 addExternal(String json) throws FoundException, BusinessException, BadRequestException;

    public OrderCli1 cancelExternal(String orderNumberCli1) throws BusinessException;
}
