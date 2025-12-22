package edu.iua.nexus.model.business.impl;

import edu.iua.nexus.auth.model.User;
import edu.iua.nexus.auth.model.IUserAuthBusiness;
import edu.iua.nexus.model.Alarm;
import edu.iua.nexus.model.Order;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.ConflictException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;
import edu.iua.nexus.model.business.interfaces.IAlarmBusiness;
import edu.iua.nexus.model.repository.AlarmRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Capa de negocio para la entidad {@link Alarm}. Centraliza la persistencia de alarmas y la
 * sincronizacion de estado con otros componentes del dominio. Todas las operaciones exponen
 * excepciones checked para que las capas superiores manejen de forma explicita errores de
 * validacion o problemas al acceder a la base de datos.
 *
 * Flujo general:
 * <ul>
 *   <li>Los metodos {@code list}, {@code load} y {@code getAllAlarmsByOrder} son lecturas
 *   directas al repositorio, envolviendo cualquier error en {@link BusinessException}.</li>
 *   <li>{@code add} y {@code update} validan la existencia previa y delegan el guardado al
 *   {@link AlarmRepository}, dejando registro en logs si algo falla.</li>
 *   <li>{@code setAlarmStatus} coordina la actualizacion de una alarma y su orden asociada,
 *   aplicando reglas de negocio que impiden cambios en estados no permitidos.</li>
 * </ul>
 */
@Service
@Slf4j
public class AlarmBusiness implements IAlarmBusiness {

    @Autowired
    private AlarmRepository alarmDAO;

    @Autowired
    private OrderBusiness orderBusiness;

    @Autowired
    IUserAuthBusiness userBusiness;

    @Override
    public List<Alarm> list() throws BusinessException {
        try {
            return alarmDAO.findAll();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    @Override
    public Alarm load(long id) throws NotFoundException, BusinessException {
        Optional<Alarm> alarmFound;
        try {
            alarmFound = alarmDAO.findById(id);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if (alarmFound.isEmpty())
            throw NotFoundException.builder().message("No se encuentra la alarma id= " + id).build();
        return alarmFound.get();
    }

    public Alarm load(long orderId, Alarm.Status status) throws NotFoundException, BusinessException {
        Optional<Alarm> alarmFound;
        try {
            alarmFound = alarmDAO.findByOrder_IdAndStatus(orderId, status);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if (alarmFound.isEmpty())
            throw NotFoundException.builder().message("No se encuentra la alarma para la orden id= " + orderId + " con status " + status).build();
        return alarmFound.get();
    }


    @Override
    public Alarm add(Alarm alarm) throws FoundException, BusinessException {

        // Se valida dos veces la existencia para mantener compatibilidad con logica previa:
        // si ya existe una alarma con el mismo id se informa el conflicto al caller.
        try {
            load(alarm.getId());
            throw FoundException.builder().message("Ya existe la Alarma id = " + alarm.getId()).build();
        } catch (NotFoundException e) {
            // log.trace(e.getMessage(), e);
        }
        try {
            load(alarm.getId());
            throw FoundException.builder().message("Ya existe la Alarma = " + alarm.getId()).build();
        } catch (NotFoundException e) {
            // log.trace(e.getMessage(), e);
        }
        try {
            return alarmDAO.save(alarm);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    @Override
    public Alarm update(Alarm alarm) throws NotFoundException, BusinessException {
        load(alarm.getId());
        try {
            return alarmDAO.save(alarm);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    @Override
    public Boolean isAlarmAccepted(Long orderId) {
        return alarmDAO.findByStatusAndOrder_Id(Alarm.Status.PENDING, orderId).isPresent();
    }

    @Override
    public List<Alarm> pendingReview() throws NotFoundException {
        Optional<List<Alarm>> alarm = alarmDAO.findByStatusAndOrder_Status(Alarm.Status.PENDING, Order.Status.REGISTERED_INITIAL_WEIGHING);
        if (alarm.isEmpty()) {
            throw new NotFoundException("No alarm found with status PENDING_REVIEW");
        }
        return alarm.get();
    }

    @Override
    public Page<Alarm> getAllAlarmsByOrder(Order order, Pageable pageable) throws NotFoundException, BusinessException {
        Optional<Page<Alarm>> alarms;

        try {
            alarms = alarmDAO.findAllByOrder(order, pageable);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }

        if (alarms.isEmpty()) {
            throw new NotFoundException("No alarms found for order id = " + order.getId());
        }

        return alarms.orElseGet(Page::empty);
    }

    @Override
    public Order setAlarmStatus(Alarm alarm, User user, Alarm.Status newStatus) throws BusinessException, NotFoundException, ConflictException {
        // Flujo principal de cambio de estado:
        // 1. Se obtiene la alarma y la orden actual para garantizar que existen.
        // 2. Se valida que la alarma siga pendiente y la orden siga en etapa de carga.
        // 3. Solo se aceptan transiciones a ACKNOWLEDGED o CONFIRMED_ISSUE.
        // 4. Se actualizan observaciones y usuario responsable, luego se persiste.
        Alarm alarmFound = load(alarm.getId());
        Order orderFound = orderBusiness.load(alarmFound.getOrder().getId());

        User userFound = userBusiness.load(user.getUsername());

        if (alarmFound.getStatus() != Alarm.Status.PENDING) {
            throw ConflictException.builder().message("La alarma ya fue manejada").build();
        }
        if (orderFound.getStatus() != Order.Status.REGISTERED_INITIAL_WEIGHING) {
            throw ConflictException.builder().message("La orden no se encuentra en estado de carga").build();
        }

        if (newStatus != Alarm.Status.ACKNOWLEDGED && newStatus != Alarm.Status.CONFIRMED_ISSUE) {
            throw BusinessException.builder().message("El estado proporcionado no es válido").build();
        }

        if (!(alarm.getObservation() == null || alarm.getObservation().isEmpty())) {
            alarmFound.setObservation(alarm.getObservation());
        }

        alarmFound.setStatus(newStatus);
        alarmFound.setUser(userFound);
        update(alarmFound);

        return orderBusiness.update(orderFound);
    }


}
