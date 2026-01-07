package edu.iua.nexus.model.business.interfaces;

import edu.iua.nexus.auth.model.User;
import edu.iua.nexus.model.Alarm;
import edu.iua.nexus.model.Order;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.ConflictException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IAlarmBusiness {

    public List<Alarm> list() throws BusinessException;

    public Alarm load(long id) throws NotFoundException, BusinessException;


    public Alarm add(Alarm alarm) throws FoundException, BusinessException;

    Alarm update(Alarm alarm) throws NotFoundException, BusinessException;

    Boolean pendingAlarmExists(Long orderId) throws BusinessException;

    List<Alarm> pendingReview() throws NotFoundException;

    Page<Alarm> getAllAlarmsByOrder(Order order, Pageable pageable) throws NotFoundException, BusinessException;

    Order setAlarmStatus(Alarm alarm, User user, Alarm.Status newStatus) throws BusinessException, NotFoundException, ConflictException;

}
