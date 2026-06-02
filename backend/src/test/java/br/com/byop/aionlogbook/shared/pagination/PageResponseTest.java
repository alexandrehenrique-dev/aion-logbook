package br.com.byop.aionlogbook.shared.pagination;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    void shouldCreatePageResponseFromSpringPage() {
        Page<String> page = new PageImpl<>(
                List.of("A", "B"),
                PageRequest.of(1, 2),
                5
        );

        PageResponse<String> response = PageResponse.from(page);

        assertThat(response.data()).containsExactly("A", "B");
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.pageSize()).isEqualTo(2);
        assertThat(response.total()).isEqualTo(5);
        assertThat(response.totalPages()).isEqualTo(3);
    }
}
