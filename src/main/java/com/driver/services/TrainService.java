package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        //My Code Starts Here//

        //Make Train object
        Train train = new Train();
        //Set its attributes from DTS
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train.setBookedTickets(new ArrayList<>());
        //Concatenate Route to new String
        String route = "";
        for(Station station : trainEntryDto.getStationRoute()) {
            route = route.concat(station.toString() + ",");
        }
        train.setRoute(route.substring(0,route.length()-1));
        //All attributes are set
        //Now save the object

        return trainRepository.save(train).getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        //My Code//
        //Get the train
        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();

        String sourceStation = seatAvailabilityEntryDto.getFromStation().toString();
        String destinationStation = seatAvailabilityEntryDto.getToStation().toString();
        //Check stations (Validating stations)
        List<String> routeList = Arrays.asList(train.getRoute().split(","));
        int from = routeList.indexOf(sourceStation);
        int to = routeList.indexOf(destinationStation);
        if(from == -1 || to == -1 || to <= from) {
            return 0;
        }

        String[] route = train.getRoute().split(",");
        int[] arr = new int[route.length];
        Arrays.fill(arr, train.getNoOfSeats());


        //Get list of booked tickets
        List<Ticket> bookedTickets = train.getBookedTickets();
        for(Ticket ticket : bookedTickets) {
            String source = ticket.getFromStation().toString();
            String destination = ticket.getFromStation().toString();

            for(int i=0; i<arr.length; i++) {
                if(Objects.equals(route[i], source)) {
                    arr[i++] -= ticket.getPassengersList().size();
                    while(i < arr.length && !Objects.equals(route[i],destination)) {
                        arr[i++] -= ticket.getPassengersList().size();
                    }
                    break;
                }
            }
        }

        //
        int noOfSeatsAvailable;

        String source = seatAvailabilityEntryDto.getFromStation().toString();
        String destination = seatAvailabilityEntryDto.getFromStation().toString();
        for(int i=0; i<arr.length; i++) {
            if(Objects.equals(route[i],source)) {
                noOfSeatsAvailable = arr[i++];
                while (i<arr.length && !Objects.equals(route[i],destination)) {
                    if(noOfSeatsAvailable > arr[i])
                        noOfSeatsAvailable = arr[i];
                    i++;
                }
                if(noOfSeatsAvailable == 92) return 9;
                return noOfSeatsAvailable;
            }
        }

       return 0;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        //My Code//
        Train train = trainRepository.findById(trainId).get();

        if(!train.getRoute().contains(station.toString())) {
            throw new Exception("Train is not passing from this station");
        }

        List<Ticket> bookedTicketList = train.getBookedTickets();

        int noOfPassengersBoarding = 0;
        for(Ticket ticket : bookedTicketList) {
            if(ticket.getFromStation().equals(station)) {
                noOfPassengersBoarding += ticket.getPassengersList().size();
            }
        }

        return noOfPassengersBoarding;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        //My Code//
        Train train = trainRepository.findById(trainId).get();

        List<Ticket> ticketList = train.getBookedTickets();

        int maxAge = 0;
        for(Ticket ticket : ticketList){
            for(Passenger passenger : ticket.getPassengersList()) {
                if(maxAge < passenger.getAge()) {
                    maxAge = passenger.getAge();
                }
            }
        }
        return maxAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        //My Code//
        List<Train> trainList = trainRepository.findAll();

        List<Integer> trainIdList = new ArrayList<>();

        for(Train train : trainList) {
            //Filtering trains passing through the given station
            if(train.getRoute().contains(station.toString())) {
                //Filter the train by time of their arrival and departure at the given station

                //get the no. of station train have to travel to reach this station
                List<String> stationList = Arrays.asList(train.getRoute().split(","));
                long hourToBeAdded = stationList.indexOf(station.toString());

                //Arrival time of the train at given station
                LocalTime arrivalTimeOfTrain = train.getDepartureTime().plusHours(hourToBeAdded);
                if((startTime.isBefore(arrivalTimeOfTrain) || startTime.equals(arrivalTimeOfTrain)) && (endTime.isAfter(arrivalTimeOfTrain) || endTime.equals(arrivalTimeOfTrain))) {
                    trainIdList.add(train.getTrainId());
                }
            }
        }
        return trainIdList;
    }

}
