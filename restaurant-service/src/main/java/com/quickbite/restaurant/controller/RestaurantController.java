package com.quickbite.restaurant.controller;

import com.quickbite.restaurant.dto.RestaurantRequest;
import com.quickbite.restaurant.entity.Restaurant;
import com.quickbite.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<Restaurant> register(
            @Valid @RequestBody RestaurantRequest request,
            @RequestHeader("X-User-Id") String userId) {

        Long recipientId = Long.parseLong(userId);

        Restaurant restaurant = restaurantService.registerRestaurant(request, recipientId);
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurant);
    }

    // GET /api/restaurants/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getById(id));
    }

    // GET /api/restaurants/all
    @GetMapping("/all")
    public ResponseEntity<List<Restaurant>> getAllApproved() {
        return ResponseEntity.ok(restaurantService.getAllApproved());
    }

    // GET /api/restaurants/city/{city}
    @GetMapping("/city/{city}")
    public ResponseEntity<List<Restaurant>> getByCity(@PathVariable String city) {
        return ResponseEntity.ok(restaurantService.getByCity(city));
    }

    // GET /api/restaurants/cuisine/{cuisine}
    @GetMapping("/cuisine/{cuisine}")
    public ResponseEntity<List<Restaurant>> getByCuisine(@PathVariable String cuisine) {
        return ResponseEntity.ok(restaurantService.getByCuisine(cuisine));
    }

    // GET /api/restaurants/search?name=pizza
    @GetMapping("/search")
    public ResponseEntity<List<Restaurant>> search(@RequestParam String name) {
        return ResponseEntity.ok(restaurantService.searchByName(name));
    }

    // GET /api/restaurants/nearby?lat=28.6&lng=77.2&radius=5
    @GetMapping("/nearby")
    public ResponseEntity<List<Restaurant>> getNearby(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "5.0") Double radius) {
        return ResponseEntity.ok(restaurantService.getNearby(lat, lng, radius));
    }

    // GET /api/restaurants/my — owner sees their restaurants
    @GetMapping("/my")
    public ResponseEntity<List<Restaurant>> getMyRestaurants(
            @RequestHeader("X-User-Id") String userId) {
        Long recipientId = Long.parseLong(userId);
        return ResponseEntity.ok(restaurantService.getByOwner(recipientId));
    }

    // GET /api/restaurants/pending — admin sees pending restaurants
    @GetMapping("/pending")
    public ResponseEntity<List<Restaurant>> getPending() {
        return ResponseEntity.ok(restaurantService.getPendingApproval());
    }

    // PUT /api/restaurants/approve/{id} — admin approves
    @PutMapping("/approve/{id}")
    public ResponseEntity<Restaurant> approve(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.approveRestaurant(id));
    }

    // PUT /api/restaurants/toggle/{id} — owner opens/closes
    @PutMapping("/toggle/{id}")
    public ResponseEntity<Restaurant> toggleOpen(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.toggleOpen(id));
    }

    //owner updates details
    @PutMapping("/{id}")
    public ResponseEntity<Restaurant> update(
            @PathVariable Long id, @Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, request));
    }

    // DELETE /api/restaurants/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok("Restaurant deleted");
    }
}