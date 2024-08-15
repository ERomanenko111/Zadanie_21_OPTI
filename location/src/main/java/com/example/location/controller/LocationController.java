package com.example.location.controller;

import com.example.location.model.Geodata;
import com.example.location.repository.GeodataRepository;
import com.example.location.model.Weather;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/location")
public class LocationController {

    @Autowired
    private GeodataRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping
    public ResponseEntity<List<Geodata>> getAllLocations() {
        List<Geodata> locations = (List<Geodata>) repository.findAll();
        return ResponseEntity.ok(locations);
    }

    @GetMapping(params = "name")
    public ResponseEntity<Geodata> getLocationByName(@RequestParam String name) {
        Optional<Geodata> geodata = repository.findByName(name);
        return geodata.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Geodata> save(@RequestBody Geodata geodata) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(geodata));
    }

    @PutMapping(params = "name")
    public ResponseEntity<Geodata> updateLocation(@RequestParam String name, @RequestBody Geodata newGeodata) {
        Optional<Geodata> optionalGeodata = repository.findByName(name);
        if (optionalGeodata.isPresent()) {
            newGeodata.setId(optionalGeodata.get().getId()); // Используем существующий id
            return ResponseEntity.ok(repository.save(newGeodata));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping(params = "name")
    public ResponseEntity<Void> deleteLocation(@RequestParam String name) {
        Optional<Geodata> optionalGeodata = repository.findByName(name);
        if (optionalGeodata.isPresent()) {
            repository.delete(optionalGeodata.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/weather")
    public ResponseEntity<Weather> getWeatherForLocation(@RequestParam String name) {
        Optional<Geodata> optionalGeodata = repository.findByName(name);
        if (optionalGeodata.isPresent()) {
            Geodata geodata = optionalGeodata.get();
            String url = String.format("http://localhost:8082/?lat=%s&lon=%s", geodata.getLat(), geodata.getLon());
            return new ResponseEntity<>(restTemplate.getForObject(url, Weather.class), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}