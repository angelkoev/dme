package com.akoev.dme.movies;

public record Title(Long id, String name, Genre genre, int durationMinutes) {
}
