package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;

    @Autowired
    TrainService trainService;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        //My Code Starts Here//
        //Getting train from DB
        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();

        //Checking seat availability

        SeatAvailabilityEntryDto seatAvailabilityEntryDto = new SeatAvailabilityEntryDto();
        seatAvailabilityEntryDto.setTrainId(bookTicketEntryDto.getTrainId());
        seatAvailabilityEntryDto.setFromStation(bookTicketEntryDto.getFromStation());
        seatAvailabilityEntryDto.setToStation(bookTicketEntryDto.getToStation());
        int noOfAvailableSeats = trainService.calculateAvailableSeats(seatAvailabilityEntryDto);

        if(noOfAvailableSeats < bookTicketEntryDto.getNoOfSeats()) {
//            throw new Exception("Less tickets are available");
            throw new Exception("Less tickets available");
        }


        //Seats are available so, book the ticket

        Passenger bookingPerson = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        //Making the passengers list
        List<Passenger> passengerList = new ArrayList<>();
        for(int id : bookTicketEntryDto.getPassengerIds()) {
            passengerList.add(passengerRepository.findById(id).get());
        }


        //
        Ticket ticket = new Ticket();
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation((bookTicketEntryDto.getToStation()));
        ticket.setPassengersList(passengerList);
        ticket.setTrain(train);
        //calculate fare and set
        String sourceStation = bookTicketEntryDto.getFromStation().toString();
        String destinationStation = bookTicketEntryDto.getToStation().toString();
        ticket.setTotalFare(trainService.calculateFare(train.getTrainId(),sourceStation,destinationStation)*bookTicketEntryDto.getNoOfSeats());

        bookingPerson.getBookedTickets().add(ticket);
        train.getBookedTickets().add(ticket);

        //
        trainRepository.save(train);
//        ticketRepository.save(ticket);
//        passengerRepository.save(bookingPerson);

       return ticketRepository.save(ticket).getTicketId();

    }
}
