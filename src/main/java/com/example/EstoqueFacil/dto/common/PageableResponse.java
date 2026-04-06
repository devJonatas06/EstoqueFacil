package com.example.EstoqueFacil.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta paginada para listagens")
public class PageableResponse<T> {

    @Schema(description = "Lista de itens da página atual")
    private List<T> content;

    @Schema(description = "Número da página atual (0-indexado)", example = "0")
    private int page;

    @Schema(description = "Quantidade de itens por página", example = "20")
    private int size;

    @Schema(description = "Total de elementos em todas as páginas", example = "150")
    private long totalElements;

    @Schema(description = "Total de páginas", example = "8")
    private int totalPages;

    @Schema(description = "Indica se é a primeira página", example = "true")
    private boolean first;

    @Schema(description = "Indica se é a última página", example = "false")
    private boolean last;

    public static <T> PageableResponse<T> fromPage(Page<T> page) {
        return PageableResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}