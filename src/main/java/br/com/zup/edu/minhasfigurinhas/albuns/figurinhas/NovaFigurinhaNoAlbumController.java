package br.com.zup.edu.minhasfigurinhas.albuns.figurinhas;

import br.com.zup.edu.minhasfigurinhas.albuns.Album;
import br.com.zup.edu.minhasfigurinhas.albuns.AlbumRepository;
import br.com.zup.edu.minhasfigurinhas.albuns.Figurinha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.net.URI;

@RestController
public class NovaFigurinhaNoAlbumController {

    @Autowired
    private AlbumRepository repository;

    @Transactional
    @PostMapping("/api/albuns/{albumId}/figurinhas")
    public ResponseEntity<?> adicionaFigurinha(@PathVariable("albumId") Long albumId,
                                               @RequestBody @Valid NovaFigurinhaNoAlbumRequest request,
                                               @AuthenticationPrincipal Jwt principalUser,
                                               UriComponentsBuilder uriBuilder) {

        Album album = repository.findById(albumId).orElseThrow(() -> {
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "album não encontrado");
        });

        String username = principalUser.getClaim("preferred_username");
        if (!username.equals(album.getDono())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Um usuário não pode alterar os álbuns de outro usuário");
        }

        Figurinha figurinha = request.toModel();
        album.adiciona(figurinha);

        repository.flush();

        URI location = uriBuilder
                    .path("/api/albuns/{albumId}/figurinhas/{figurinhaId}")
                    .buildAndExpand(album.getId(), figurinha.getId())
                    .toUri();

        return ResponseEntity
                .created(location).build(); // HTTP 201
    }
}
