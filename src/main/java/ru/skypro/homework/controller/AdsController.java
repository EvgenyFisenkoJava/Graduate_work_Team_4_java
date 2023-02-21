package ru.skypro.homework.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.entity.Ads;
import ru.skypro.homework.entity.Image;
import ru.skypro.homework.impl.CommentServiceImpl;
import ru.skypro.homework.impl.ImageServiceImpl;
import ru.skypro.homework.repository.AdsRepository;
import ru.skypro.homework.service.AdsService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequestMapping("/ads")
@CrossOrigin(value = "http://localhost:3000")
@RestController
public class AdsController {

    private final CommentServiceImpl commentService;
    private final AdsService adsService;
    private final ImageServiceImpl imageService;
    private final AdsRepository adsRepository;

    AdsController(CommentServiceImpl commentService, AdsService adsService, ImageServiceImpl imageService,
                  AdsRepository adsRepository, AdsRepository adsRepository1) {
        this.commentService = commentService;
        this.adsService = adsService;
        this.imageService = imageService;
        this.adsRepository = adsRepository1;
    }

    // /ads
    @GetMapping("")
    public ResponseWrapperAdsDto getALLAds() {
        return adsService.getAll();
    }

    @PostMapping(value = "",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public AdsDto addAds(@RequestPart(required = true) CreateAdsDto properties,
                         @RequestPart(required = false) MultipartFile image)
            throws IOException {
        // создаем сущность Ads
       Long id =  adsService.addAds(properties).getPk(); // сохранили в базу, вытащили id

        // сохраняем сущность Image
        imageService.uploadImage(id, image);

        // добавили картинку к Ads
        List<Image> images = new ArrayList<Image>();
        images.add(imageService.findImage(id));
        Ads reWrite = Objects.requireNonNull(adsRepository.findById(id).orElse(null));
        reWrite.setImage(images);
        adsRepository.save(reWrite);

        return adsService.getAds(id);
    }

    // /ads/{id}
    @GetMapping("{id}")
    public FullAdsDto getAds(@PathVariable Long id) {
        return adsService.getFullAds(id);
    }

    @DeleteMapping("{id}")
    public FullAdsDto removeAds(@PathVariable Long id) {
        return adsService.removeFullAds(id);
    }

    @PatchMapping("{id}")
    public AdsDto updateAds(@PathVariable Long id, @RequestBody CreateAdsDto ads) {
        return adsService.updateAds(id, ads);
    }


    // {ad_pk}/comments/{id}
    @GetMapping("{adPk}/comments/{id}")
    public ResponseEntity<CommentDto> getComments(@PathVariable Long adPk, @PathVariable Long id) {
        CommentDto commentDto = commentService.getComments(id);
        if (commentDto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(commentDto);
    }

    @DeleteMapping("{adPk}/comments/{id}")
    public ResponseEntity<Void> deleteComments(@PathVariable Long adPk,
                                               @PathVariable Long id) {
        commentService.deleteComments(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("{adPk}/comments/{id}")
    public ResponseEntity<CommentDto> updateComments(@PathVariable Long adPk,
                                                     @PathVariable Long id,
                                                     @RequestBody CommentDto comment) {
        CommentDto commentDto = commentService.updateComments(adPk, id, comment);
        if (commentDto == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(commentDto);
    }

    @GetMapping("me")
    public ResponseWrapperAdsDto getAdsMe() {
        return new ResponseWrapperAdsDto();
    }

    @PostMapping(value = "{adsPk}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity <String> uploadImage (@PathVariable Long adsPk, @RequestParam MultipartFile image) throws IOException {
        imageService.uploadImage(adsPk, image);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public void updateAdsImage (@PathVariable Long id, HttpServletResponse response) throws IOException {
        Image image = imageService.findImage(id);

        Path path = Path.of(image.getFilePath());

        try (InputStream is = Files.newInputStream(path);
             OutputStream os = response.getOutputStream()) {

            response.setStatus(200);
            response.setContentType(image.getMediaType());
            response.setContentLength((int)image.getFileSize());
            is.transferTo(os);
        }
    }
    // метод поиска для тренировка
    @GetMapping("/search")
    public Ads searchAds (@RequestParam Long part) {
       return adsService.getAdsNotDtoById(part);
    }
}

