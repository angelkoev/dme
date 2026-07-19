package com.akoev.dme.movies;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TitleCatalog {

    private static final List<Title> TITLES = List.of(
            new Title(1L, "Skyline Heist", Genre.ACTION, 118),
            new Title(2L, "Last Stand at Dawn", Genre.ACTION, 135),
            new Title(3L, "The Wedding Mixup", Genre.COMEDY, 98),
            new Title(4L, "Office Chaos", Genre.COMEDY, 92),
            new Title(5L, "Quiet Winters", Genre.DRAMA, 124),
            new Title(6L, "The Long Goodbye", Genre.DRAMA, 141),
            new Title(7L, "Beyond the Stars", Genre.SCI_FI, 132),
            new Title(8L, "Colony Nine", Genre.SCI_FI, 110),
            new Title(9L, "The Old House", Genre.HORROR, 101),
            new Title(10L, "Midnight Visitor", Genre.HORROR, 94),
            new Title(11L, "Our Blue Planet", Genre.DOCUMENTARY, 88),
            new Title(12L, "The Rise of Cities", Genre.DOCUMENTARY, 105),
            new Title(13L, "Forest Friends", Genre.ANIMATION, 90),
            new Title(14L, "Robot Kingdom", Genre.ANIMATION, 96),
            new Title(15L, "Heist Squad Returns", Genre.ACTION, 128)
    );

    public List<Title> findAll() {
        return TITLES;
    }
}
