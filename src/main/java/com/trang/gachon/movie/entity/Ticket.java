package com.trang.gachon.movie.entity;

import com.trang.gachon.movie.enums.TicketType;
import com.trang.gachon.movie.converter.TicketTypeConverter;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "ticket")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

    //giá vé : 45-75k
    @Column(name = "price", nullable = false)
    private Long price;

    //db lưu int 1/2 -> dùng convert map sang ticketType enum
    @Convert(converter = TicketTypeConverter.class )
    @Column(name = "ticket_type", nullable = false)
    private TicketType ticketType;
}
