package com.gal.usc.roomify.Tasks;

import com.gal.usc.roomify.model.Falta;
import com.gal.usc.roomify.model.Reserva;
import com.gal.usc.roomify.model.Severidad;
import com.gal.usc.roomify.model.Usuario;
import com.gal.usc.roomify.repository.FaltaRepository;
import com.gal.usc.roomify.repository.ReservaRepository;
import com.gal.usc.roomify.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


@Component
public class ScheduledTasks {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FaltaRepository faltaRepository;
    private final ObjectMapper mapper;
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public ScheduledTasks(ReservaRepository reservaRepository, UsuarioRepository usuarioRepository, FaltaRepository faltaRepository, ObjectMapper mapper) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.faltaRepository = faltaRepository;
        this.mapper = mapper;
    }

    @Scheduled(fixedRate = 20000)
    public void asignarFaltas() {
        List<Reserva> reservas = reservaRepository.findAll();
        for (Reserva reserva : reservas) {
            // Si la reserva se pasa de hora, se pone falta al usuario y se destruye la reserva
            if( reserva.horaFin().isBefore(LocalDateTime.now()))
            {
                Usuario castigado = reserva.usuario();
                Falta falta = new Falta("N1","Ha entregado tarde la llave de la sala " + reserva.sala().getId(), LocalDate.now(), Severidad.LEVE,LocalDate.now().plusMonths(6), castigado);
                faltaRepository.save(falta);
                reservaRepository.delete(reserva);

            }
        }
    }

    // TODO: Por eficiencia y sentido, Esta comprobación debería hacerse una vez al día.
    @Scheduled(fixedRate = 20000)
    public void limpiarFaltasCaducadas(){
        List<Falta> faltas = faltaRepository.findAll();
        // Buscamos todas las faltas caducadas y las eliminamos
        for (Falta falta : faltas) {
            if(falta.fechaCaducidad().isBefore(LocalDate.now())){
                faltaRepository.delete(falta);
            }
        }
    }

}
