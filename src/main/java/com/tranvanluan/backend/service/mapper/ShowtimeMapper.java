package com.tranvanluan.backend.service.mapper;

import com.tranvanluan.backend.dto.ShowtimeDTO;
import com.tranvanluan.backend.entity.Showtime;
import org.springframework.stereotype.Component;

@Component
public class ShowtimeMapper {

    public ShowtimeDTO toDTO(Showtime showtime) {
        return ShowtimeDTO.builder()
                .id(showtime.getId())
                .showDate(showtime.getShowDate())
                .showTime(showtime.getShowTime())
                .price(showtime.getPrice())
                .movieId(showtime.getMovie().getId())
                .movieTitle(showtime.getMovie().getTitle())
                .roomId(showtime.getRoom().getId())
                .roomName(showtime.getRoom().getName())
                .build();
    }
}
