package com.trang.gachon.movie.entity;

import com.trang.gachon.movie.enums.TicketStatus;
import com.trang.gachon.movie.enums.TicketType;
import com.trang.gachon.movie.converter.TicketTypeConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


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

    @ManyToOne(fetch = FetchType.LAZY)    //1 invoice có nhiều ticket, nhưng 1 ticket thì có 1 invoice 
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_seat_id", nullable = false)
    private ScheduleSeat scheduleSeat;
    

    //db lưu int 1/2 -> dùng convert map sang ticketType enum
    @Convert(converter = TicketTypeConverter.class )
    @Column(name = "ticket_type", nullable = false)
    private TicketType ticketType;
}
