package br.com.zup.edu.minhasfigurinhas.albuns;

import base.SpringBootIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

class NovoAlbumControllerTest extends SpringBootIntegrationTest {

    @Autowired
    private AlbumRepository repository;

    @BeforeEach
    private void setUp() {
        repository.deleteAll();
    }

    @Test
    public void deveCadastrarNovoAlbumComSuasFigurinhas() throws Exception {
        // cenário
        NovoAlbumRequest novoAlbum = new NovoAlbumRequest("CDZ",
                "Cavaleiros do Zodiaco",
                List.of(
                        new NovaFigurinhaRequest("Seya", "http://animes.com/cdz/seya.png"),
                        new NovaFigurinhaRequest("Hyoga", "http://animes.com/cdz/hyoga.png")
                )
        );

        // ação
        mockMvc.perform(POST("/api/albuns", novoAlbum)
                        .with(jwt()
                                .jwt(jwt -> {
                                    jwt.claim("preferred_username", "rponte");
                                })
                                .authorities(new SimpleGrantedAuthority("SCOPE_albuns:write"))
                        ))
                .andExpect(status().isCreated())
                .andExpect(redirectedUrlPattern("**/api/albuns/*"))
                ;

        // validação
        assertEquals(1, repository.count(), "total de albuns");
        assertEquals("rponte", repository.findAll().get(0).getDono(), "dono do álbum");
    }

    @Test
    public void naoDeveCadastrarNovoAlbumComSuasFigurinhas_quandoParametrosInvalidos() throws Exception {
        // cenário
        NovoAlbumRequest albumInvalido = new NovoAlbumRequest("", "", null);

        // ação
        mockMvc.perform(POST("/api/albuns", albumInvalido)
                        .with(jwt()
                                .jwt(jwt -> {
                                    jwt.claim("preferred_username", "rponte");
                                })
                                .authorities(new SimpleGrantedAuthority("SCOPE_albuns:write"))
                        ))
                .andExpect(status().isBadRequest())
        ;

        // validação
        assertEquals(0, repository.count(), "total de albuns");
    }

    @Test
    public void naoDeveCadastrarNovoAlbumComSuasFigurinhasQuandoAccessTokenNaoEnviado() throws Exception {
        // cenário
        NovoAlbumRequest novoAlbum = new NovoAlbumRequest("CDZ",
                "Cavaleiros do Zodiaco",
                List.of(
                        new NovaFigurinhaRequest("Seya", "http://animes.com/cdz/seya.png"),
                        new NovaFigurinhaRequest("Hyoga", "http://animes.com/cdz/hyoga.png")
                )
        );

        // ação
        mockMvc.perform(POST("/api/albuns", novoAlbum))
                .andExpect(status().isUnauthorized())
        ;

        // validação
        assertEquals(0, repository.count(), "total de albuns");
    }

    @Test
    public void naoDeveCadastrarNovoAlbumComSuasFigurinhasQuandoAccessTokenNaoPossuiScopeApropriado() throws Exception {
        // cenário
        NovoAlbumRequest novoAlbum = new NovoAlbumRequest("CDZ",
                "Cavaleiros do Zodiaco",
                List.of(
                        new NovaFigurinhaRequest("Seya", "http://animes.com/cdz/seya.png"),
                        new NovaFigurinhaRequest("Hyoga", "http://animes.com/cdz/hyoga.png")
                )
        );

        // ação
        mockMvc.perform(POST("/api/albuns", novoAlbum).with(jwt()
                        .jwt(jwt -> {
                            jwt.claim("preferred_username", "rponte");
                        })
                ))
                .andExpect(status().isForbidden())
        ;

        // validação
        assertEquals(0, repository.count(), "total de albuns");
    }

}