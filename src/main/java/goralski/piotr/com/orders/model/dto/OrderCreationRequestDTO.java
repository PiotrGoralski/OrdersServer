package goralski.piotr.com.orders.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class OrderCreationRequestDTO {

    @NotBlank
    private String description;
    @NotNull
    private UUID userId;

}
