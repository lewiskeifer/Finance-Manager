package keifer.controller;

import io.swagger.annotations.Api;
import keifer.api.model.Card;
import keifer.api.model.Deck;
import keifer.service.DataMigrationService;
import keifer.service.DeckService;
import keifer.service.TcgService;
import lombok.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api
@RequestMapping("/manager")
@RestController
public class ManagerController {

    private final DeckService deckService;
    private final DataMigrationService dataMigrationService;
    private final TcgService tcgService;

    public ManagerController(@NonNull DeckService deckService, @NonNull DataMigrationService dataMigrationService, @NonNull TcgService tcgService) {
        this.deckService = deckService;
        this.dataMigrationService = dataMigrationService;
        this.tcgService = tcgService;
    }

    @GetMapping("/decks")
    public List<Deck> getDecks() {
        return deckService.getDecks();
    }

    @GetMapping("/decks/{deckId}")
    public Deck getDeck(@PathVariable("deckId") Long deckId) {
        return deckService.getDeck(deckId);
    }

    @PutMapping("/decks/{deckId}/card")
    public void saveCard(@PathVariable("deckId") Long deckId, @RequestBody Card card) {
        deckService.saveCard(deckId, card);
    }

    @PutMapping("/decks/{deckId}")
    public void saveDeck(@PathVariable("deckId") Long deckId, @RequestBody Deck deck) {
        deckService.saveDeck(deckId, deck);
    }

    @PutMapping("/decks/{deckId}/refresh")
    public void refreshDeck(@PathVariable("deckId") Long deckId) {
        deckService.refreshDeck(deckId);
    }

    @GetMapping("/migrate")
    public void migrate() {
        dataMigrationService.migrateData();
    }

}
