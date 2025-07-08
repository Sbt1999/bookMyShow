package com.example.bookMyShow.service;

import com.example.bookMyShow.exception.SelectedSeatsNotAvailableException;
import com.example.bookMyShow.model.ShowSeat;
import com.example.bookMyShow.model.Ticket;
import com.example.bookMyShow.model.User;
import com.example.bookMyShow.model.constant.ShowSeatStatus;
import com.example.bookMyShow.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/*
SpringBoot have very interesting concept IOC and DI
Inversion of Control -> Means giving control to SpringBoot to control the DI
 */
@Service // SpringBoot will create a service object and inject that inside it.
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ShowSeatService showSeatService;
    @Autowired
    private ShowService showService;

    public Ticket createTicket(int userId, List<Integer> showSeatIds) throws SelectedSeatsNotAvailableException {
        User user = userService.getUserById(userId);
        List<ShowSeat> showSeats = new ArrayList<>();
        int totalCost = 0;

        showSeats = checkAndLockShowSeats(showSeatIds);

        //TODO: payment done here logic
        // if payments fails then move showSeat back to available state

        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setShowSeats(showSeats);
        ticket.setShow(showSeats.getFirst().getShow());
        ticket.setTotalCost(totalCost);

        for(ShowSeat showSeat: showSeats){
            showSeat.setShowSeatStatus(ShowSeatStatus.BOOKED);
            showSeatService.updateShowSeat(showSeat);
        }

        return ticketRepository.save(ticket);
    }

    @Transactional (isolation = Isolation.SERIALIZABLE)
    public List<ShowSeat> checkAndLockShowSeats(List<Integer> showSeatIds) throws SelectedSeatsNotAvailableException {
        // CHECK IF ALL SEATS ARE AVAILABLE
        List<ShowSeat> showSeats = new ArrayList<>();
        for(int showSeatId: showSeatIds){
            ShowSeat showSeat = showSeatService.getShowSeatById(showSeatId);
            if(!showSeat.getShowSeatStatus().equals(ShowSeatStatus.AVAILABLE)){
                throw new SelectedSeatsNotAvailableException("Seats selected for booking are not available");
            }
        }
        //LOCK THE SELECTED SEATS --- the loop above and this loop cant be merged
        for(int showSeatId: showSeatIds){
            ShowSeat showSeat = showSeatService.getShowSeatById(showSeatId);
            showSeat.setShowSeatStatus(ShowSeatStatus.LOCKED);
            showSeats.add(showSeat);
            showSeatService.updateShowSeat(showSeat);
        }
        return showSeats;
    }

}
