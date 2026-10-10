package br.edu.ufape.enzitech.service;

import br.edu.ufape.enzitech.dto.request.ExperimentRequestDTO;
import br.edu.ufape.enzitech.model.Experiment;
import br.edu.ufape.enzitech.model.Treatment;
import br.edu.ufape.enzitech.model.User;
import br.edu.ufape.enzitech.repository.EnzymeRepository;
import br.edu.ufape.enzitech.repository.ExperimentEnzymeRepository;
import br.edu.ufape.enzitech.repository.ExperimentRepository;
import br.edu.ufape.enzitech.repository.ExperimentTreatmentRepository;
import br.edu.ufape.enzitech.repository.ResultExperimentRepository;
import br.edu.ufape.enzitech.repository.TreatmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnershipTest {

    @Mock private ExperimentRepository experimentRepository;
    @Mock private TreatmentRepository treatmentRepository;
    @Mock private EnzymeRepository enzymeRepository;
    @Mock private ExperimentEnzymeRepository experimentEnzymeRepository;
    @Mock private ExperimentTreatmentRepository experimentTreatmentRepository;
    @Mock private ResultExperimentRepository resultExperimentRepository;
    @Mock private ResultSignatureService resultSignatureService;

    private final User owner = user();
    private final User intruder = user();

    private ExperimentService experimentService() {
        return new ExperimentService(experimentRepository, treatmentRepository, enzymeRepository,
                experimentEnzymeRepository, experimentTreatmentRepository, resultExperimentRepository,
                resultSignatureService);
    }

    @Test
    void ownerCanReadOwnExperiment() {
        Experiment experiment = experimentOf(owner);
        when(experimentRepository.findById(experiment.getId())).thenReturn(Optional.of(experiment));

        assertThat(experimentService().findOwnedById(experiment.getId(), owner)).isSameAs(experiment);
    }

    @Test
    void otherUsersExperimentLooksLikeItDoesNotExist() {
        Experiment experiment = experimentOf(owner);
        when(experimentRepository.findById(experiment.getId())).thenReturn(Optional.of(experiment));

        assertThatThrownBy(() -> experimentService().findOwnedById(experiment.getId(), intruder))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void cannotDeleteOtherUsersExperiment() {
        Experiment experiment = experimentOf(owner);
        when(experimentRepository.findById(experiment.getId())).thenReturn(Optional.of(experiment));

        assertThatThrownBy(() -> experimentService().delete(experiment.getId(), intruder))
                .isInstanceOf(ResponseStatusException.class);

        verify(experimentRepository, never()).delete(any());
    }

    @Test
    void cannotCreateExperimentWithOtherUsersTreatment() {
        Treatment foreign = new Treatment();
        foreign.setId(UUID.randomUUID());
        foreign.setUser(owner);
        when(treatmentRepository.findAllById(any())).thenReturn(List.of(foreign));

        ExperimentRequestDTO dto = new ExperimentRequestDTO("Exp", "Desc", 3, List.of(foreign.getId()), List.of());

        assertThatThrownBy(() -> experimentService().create(dto, intruder))
                .isInstanceOf(EntityNotFoundException.class);

        verify(experimentRepository, never()).save(any());
    }

    @Test
    void cannotDeleteOtherUsersTreatment() {
        Treatment treatment = new Treatment();
        treatment.setId(UUID.randomUUID());
        treatment.setUser(owner);
        when(treatmentRepository.findById(treatment.getId())).thenReturn(Optional.of(treatment));

        TreatmentService treatmentService = new TreatmentService(treatmentRepository, experimentTreatmentRepository);

        assertThatThrownBy(() -> treatmentService.delete(treatment.getId(), intruder))
                .isInstanceOf(EntityNotFoundException.class);

        verify(treatmentRepository, never()).delete(any());
    }

    private static User user() {
        User user = new User();
        user.setId(UUID.randomUUID());
        return user;
    }

    private static Experiment experimentOf(User user) {
        Experiment experiment = new Experiment();
        experiment.setId(UUID.randomUUID());
        experiment.setUser(user);
        return experiment;
    }
}
