package de.nordwind.schulungsplaner.api;

import de.nordwind.schulungsplaner.config.KontoPrincipal;
import de.nordwind.schulungsplaner.service.TerminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/termine")
public class TerminController {
    private final TerminService termine;

    public TerminController(TerminService termine) {
        this.termine = termine;
    }

    @GetMapping
    public List<TerminService.TerminAnsicht> termine(
            @AuthenticationPrincipal KontoPrincipal konto,
            @RequestParam(required = false) String monat) {
        try {
            return termine.termine(konto.id(), monat == null || monat.isBlank() ? null : YearMonth.parse(monat));
        } catch (DateTimeParseException fehler) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Der Monat muss im Format JJJJ-MM angegeben werden.");
        }
    }

    @GetMapping("/{terminId}")
    public TerminService.TerminAnsicht details(@AuthenticationPrincipal KontoPrincipal konto,
                                               @PathVariable String terminId) {
        return termine.details(konto.id(), terminId);
    }

    @GetMapping("/enddatum-vorschlag")
    public Vorschlag vorschlag(@RequestParam String schulungId,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startdatum) {
        return new Vorschlag(termine.enddatumVorschlagen(schulungId, startdatum));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TerminService.TerminAnsicht anlegen(@AuthenticationPrincipal KontoPrincipal konto,
                                               @Valid @RequestBody TerminAnfrage anfrage) {
        return termine.anlegen(konto.id(), anfrage.alsEingabe());
    }

    @PutMapping("/{terminId}")
    public TerminService.TerminAnsicht aendern(@AuthenticationPrincipal KontoPrincipal konto,
                                               @PathVariable String terminId,
                                               @Valid @RequestBody TerminAnfrage anfrage) {
        return termine.aendern(konto.id(), terminId, anfrage.alsEingabe());
    }

    @PostMapping("/{terminId}/bestaetigung")
    public TerminService.TerminAnsicht bestaetigen(@AuthenticationPrincipal KontoPrincipal konto,
                                                   @PathVariable String terminId) {
        return termine.bestaetigen(konto.id(), terminId);
    }

    @PostMapping("/{terminId}/absage")
    public TerminService.TerminAnsicht absagen(@AuthenticationPrincipal KontoPrincipal konto,
                                               @PathVariable String terminId,
                                               @Valid @RequestBody(required = false) Absage anfrage) {
        return termine.absagen(konto.id(), terminId, anfrage == null ? null : anfrage.grund());
    }

    @DeleteMapping("/{terminId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void loeschen(@AuthenticationPrincipal KontoPrincipal konto, @PathVariable String terminId) {
        termine.loeschen(konto.id(), terminId);
    }

    @GetMapping("/{terminId}/warnung")
    public TerminService.Loeschwarnung warnung(@AuthenticationPrincipal KontoPrincipal konto,
                                               @PathVariable String terminId) {
        return termine.warnung(konto.id(), terminId);
    }

    @GetMapping("/{terminId}/traineroptionen")
    public List<TerminService.TrainerOption> traineroptionen(@AuthenticationPrincipal KontoPrincipal konto,
                                                            @PathVariable String terminId) {
        return termine.trainerOptionen(konto.id(), terminId);
    }

    @GetMapping("/traineroptionen")
    public List<TerminService.TrainerOption> traineroptionen(
            @AuthenticationPrincipal KontoPrincipal konto,
            @RequestParam String schulungId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startdatum,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate enddatum,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") java.time.LocalTime startzeit,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") java.time.LocalTime endzeit) {
        return termine.trainerOptionen(konto.id(), schulungId, startdatum, enddatum, startzeit, endzeit);
    }

    @PostMapping("/{terminId}/buchungen")
    @ResponseStatus(HttpStatus.CREATED)
    public TerminService.Teilnehmerbuchung buchung(@AuthenticationPrincipal KontoPrincipal konto,
                                                   @PathVariable String terminId,
                                                   @Valid @RequestBody Buchung anfrage) {
        return termine.buchungAnlegen(konto.id(), terminId,
                new TerminService.BuchungEingabe(anfrage.name(), anfrage.firma(), anfrage.bemerkung(), anfrage.teilnahmestatus()));
    }

    @PutMapping("/{terminId}/buchungen/{buchungId}/teilnahmestatus")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void teilnahmestatus(@AuthenticationPrincipal KontoPrincipal konto,
                                @PathVariable String terminId, @PathVariable long buchungId,
                                @RequestBody @Valid Teilnahme anfrage) {
        termine.buchungStatus(konto.id(), terminId, buchungId, anfrage.status());
    }

    @DeleteMapping("/{terminId}/buchungen/{buchungId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void buchungLoeschen(@AuthenticationPrincipal KontoPrincipal konto,
                                @PathVariable String terminId, @PathVariable long buchungId) {
        termine.buchungLoeschen(konto.id(), terminId, buchungId);
    }

    @GetMapping("/dashboard")
    public List<TerminService.DashboardEintrag> dashboard(@AuthenticationPrincipal KontoPrincipal konto) {
        return termine.dashboard(konto.id());
    }

    @GetMapping("/auswertung")
    public TerminService.Auswertung auswertung(@AuthenticationPrincipal KontoPrincipal konto) {
        return termine.auswertung(konto.id());
    }

    public record TerminAnfrage(String terminId, String schulungId, LocalDate startdatum, LocalDate enddatum,
            @com.fasterxml.jackson.annotation.JsonFormat(pattern = "HH:mm") java.time.LocalTime startzeit,
            @com.fasterxml.jackson.annotation.JsonFormat(pattern = "HH:mm") java.time.LocalTime endzeit,
            String zugangsart, String durchfuehrungsart, @Size(max = 255) String ort,
            @Size(max = 255) String kundenfirma, @Size(max = 1000) String onlineZugang,
            String trainerId, Boolean entfernenBestaetigt, String gruppeId) {
        TerminService.TerminEingabe alsEingabe() {
            return new TerminService.TerminEingabe(schulungId, startdatum, enddatum, startzeit, endzeit, zugangsart,
                    durchfuehrungsart, ort, kundenfirma, onlineZugang, trainerId,
                    Boolean.TRUE.equals(entfernenBestaetigt), gruppeId);
        }
    }
    public record Absage(@Size(max = 1000) String grund) {}
    public record Buchung(@Size(max = 255) String name, @Size(max = 255) String firma,
                          @Size(max = 1000) String bemerkung, String teilnahmestatus) {}
    public record Teilnahme(@NotNull String status) {}
    public record Vorschlag(LocalDate enddatum) {}
}
