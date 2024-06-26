package com.sysm.catalog.admin.application.category.update;

import com.sysm.catalog.admin.application.UseCaseTest;
import com.sysm.catalog.admin.domain.aggregates.category.Category;
import com.sysm.catalog.admin.domain.aggregates.category.CategoryGateway;
import com.sysm.catalog.admin.domain.aggregates.category.CategoryID;
import com.sysm.catalog.admin.domain.exceptions.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class UpdateCategoryUseCaseTest extends UseCaseTest {

    @InjectMocks
    private DefaultUpdateCategoryUseCase useCase;

    @Mock
    private CategoryGateway categoryGateway;

    @BeforeEach
    public void setUp() {
        sleep();
        Mockito.reset(categoryGateway);
    }

    //1-Test do caminho feliz
    //2-Test passaando uma propriedade invalida
    //3-Criando uma categoria inativa
    //4-Simulando um erro generico vindo do gateway
    //5-ID não exist

    @Override
    protected List<Object> getMocks() {
        return List.of(categoryGateway);
    }


    @Test
    public void givenAValidCommand_whenCallsUpdateCategory_shouldReturnCategoryId() {
        final var aCategory = Category.newCategory("Film", null, true);

        final var expectedName = "Filmes";
        final var expectedDescription = "A categoria mais assistida";
        final var expectedIsActive = true;
        final var expectedId = aCategory.getId();

        final var aCommand = UpdateCategoryCommand.with(
                expectedId.getValue(),
                expectedName,
                expectedDescription,
                expectedIsActive
        );

        when(categoryGateway.findById(eq(expectedId)))
                .thenReturn(Optional.of(aCategory.clone()));

        when(categoryGateway.update(any()))
                .thenAnswer(returnsFirstArg());

        sleep(10);
        final var actualOutput = useCase.execute(aCommand).get();


        Assertions.assertNotNull(actualOutput);
        Assertions.assertNotNull(actualOutput.id());

        Mockito.verify(categoryGateway, times(1)).findById(eq(expectedId));

        var captor = ArgumentCaptor.forClass(Category.class);
        Mockito.verify(categoryGateway, times(1)).update(captor.capture());
        var aUpdatedCategory = captor.getValue();
        assertEquals(expectedName, aUpdatedCategory.getName());
        assertEquals(expectedDescription, aUpdatedCategory.getDescription());
        assertEquals(expectedIsActive, aUpdatedCategory.isActive());
        assertEquals(expectedId, aUpdatedCategory.getId());
        assertEquals(aCategory.getCreatedAt(), aUpdatedCategory.getCreatedAt());
        Assertions.assertTrue(aCategory.getUpdatedAt().isBefore(aUpdatedCategory.getUpdatedAt()));
        Assertions.assertNull(aUpdatedCategory.getDeletedAt());

//        Mockito.verify(categoryGateway, times(1)).update(argThat(
//                aUpdatedCategory ->
//                        Objects.equals(expectedName, aUpdatedCategory.getName())
//                                && Objects.equals(expectedDescription, aUpdatedCategory.getDescription())
//                                && Objects.equals(expectedIsActive, aUpdatedCategory.isActive())
//                                && Objects.equals(expectedId, aUpdatedCategory.getId())
//                                && Objects.equals(aCategory.getCreatedAt(), aUpdatedCategory.getCreatedAt())
//                                && aCategory.getUpdatedAt().isBefore(aUpdatedCategory.getUpdatedAt())
//                                && Objects.isNull(aUpdatedCategory.getDeletedAt())
//        ));
    }

    @Test
    public void givenAnInvalidName_whenCallsUpdateCategory_thenShouldReturnDomainException(){
        final var aCategory =
                Category.newCategory("Film", null, true);
        final var expectedId = aCategory.getId();
        final String expectedName = null;
        final var expectedDescription = "A categoria mais assistida";
        final var expectedActive = false;

        final var expectedErrorMessage = "'name' should not be null";
        final var expectedErrorCount = 1;

        final var aCommand = UpdateCategoryCommand.with(expectedId.getValue(), expectedName, expectedDescription,
                expectedActive);

        when(categoryGateway.findById(eq(expectedId)))
                .thenReturn(Optional.of(aCategory.clone()));

        var notification = useCase.execute(aCommand).getLeft();

        assertEquals(expectedErrorCount, notification.getErrors().size());
        assertEquals(expectedErrorMessage, notification.firstError().getMessage());

        verify(categoryGateway, times(0)).update(any());
    }

    @Test
    public void givenAValidCommandWithInactiveCategory_whenCallsUpdateCategory_shouldReturnInactiveCategoryId() {
        final var aCategory = Category.newCategory("Film", null, true);
        final var expectedName = "Filmes";
        final var expectedDescription = "A categoria mais assistida";
        final var expectedIsActive = false;
        final var expectedId = aCategory.getId();

        final var aCommand = UpdateCategoryCommand.with(
                expectedId.getValue(),
                expectedName,
                expectedDescription,
                expectedIsActive
        );

        when(categoryGateway.findById(eq(expectedId)))
                .thenReturn(Optional.of(aCategory.clone()));

        when(categoryGateway.update(any()))
                .thenAnswer(returnsFirstArg());

        Assertions.assertTrue(aCategory.isActive());
        Assertions.assertNull(aCategory.getDeletedAt());

        sleep(10);
        final var actualOutput = useCase.execute(aCommand).get();

        Assertions.assertNotNull(actualOutput);
        Assertions.assertNotNull(actualOutput.id());

        Mockito.verify(categoryGateway, times(1)).findById(eq(expectedId));

        var captor = ArgumentCaptor.forClass(Category.class);
        Mockito.verify(categoryGateway, times(1)).update(captor.capture());
        var aUpdatedCategory = captor.getValue();
        assertEquals(expectedName, aUpdatedCategory.getName());
        assertEquals(expectedDescription, aUpdatedCategory.getDescription());
        assertEquals(expectedIsActive, aUpdatedCategory.isActive());
        assertEquals(expectedId, aUpdatedCategory.getId());
        assertEquals(aCategory.getCreatedAt(), aUpdatedCategory.getCreatedAt());
        Assertions.assertTrue(aCategory.getUpdatedAt().isBefore(aUpdatedCategory.getUpdatedAt()));
        Assertions.assertNotNull(aUpdatedCategory.getDeletedAt());

//        Mockito.verify(categoryGateway, times(1)).update(argThat(
//                aUpdatedCategory ->
//                        Objects.equals(expectedName, aUpdatedCategory.getName())
//                                && Objects.equals(expectedDescription, aUpdatedCategory.getDescription())
//                                && Objects.equals(expectedIsActive, aUpdatedCategory.isActive())
//                                && Objects.equals(expectedId, aUpdatedCategory.getId())
//                                && Objects.equals(aCategory.getCreatedAt(), aUpdatedCategory.getCreatedAt())
//                                && aCategory.getUpdatedAt().isBefore(aUpdatedCategory.getUpdatedAt())
//                                && Objects.nonNull(aUpdatedCategory.getDeletedAt())
//        ));
    }

    @Test
    public void givenAValidCommandWhenGatewayThrowsRandomException_whenCallsUpdateCategory_shouldReturnAException() {
        final var aCategory = Category.newCategory("Film", null, true);
        final var expectedId = aCategory.getId();
        var expectedName = "Filme";
        var expectedDescription = "Categoria mais assistida";
        var expectedActive = true;
        final var expectedErrorMessage = "Gateway error";
        final var expectedErrorCount = 1;

        final var aCommand = UpdateCategoryCommand.with(
                expectedId.getValue(),
                expectedName,
                expectedDescription,
                expectedActive
        );

        sleep();

        when(categoryGateway.findById(eq(expectedId)))
                .thenReturn(Optional.of(aCategory.clone()));

        when(categoryGateway.update(any()))
                .thenThrow(new IllegalStateException(expectedErrorMessage));

        var notification = useCase.execute(aCommand).getLeft();

        assertEquals(expectedErrorCount, notification.getErrors().size());
        assertEquals(expectedErrorMessage, notification.firstError().getMessage());

        final var captor = ArgumentCaptor.forClass(Category.class);
        Mockito.verify(categoryGateway, times(1)).update(captor.capture());
        final var aUpdatedCategory = captor.getValue();
        Assertions.assertEquals(expectedName, aUpdatedCategory.getName());
        Assertions.assertEquals(expectedDescription, aUpdatedCategory.getDescription());
        Assertions.assertEquals(expectedActive, aUpdatedCategory.isActive());
        Assertions.assertEquals(expectedId, aUpdatedCategory.getId());
        Assertions.assertEquals(aCategory.getCreatedAt(), aUpdatedCategory.getCreatedAt());
        Assertions.assertTrue(aCategory.getUpdatedAt().isBefore(aUpdatedCategory.getUpdatedAt()));
        Assertions.assertNull(aUpdatedCategory.getDeletedAt());

//        Mockito.verify(categoryGateway, times(1)).update(argThat(
//                aUpdatedCategory ->
//                        Objects.equals(expectedName, aUpdatedCategory.getName())
//                                && Objects.equals(expectedDescription, aUpdatedCategory.getDescription())
//                                && Objects.equals(expectedActive, aUpdatedCategory.isActive())
//                                && Objects.equals(expectedId, aUpdatedCategory.getId())
//                                && Objects.equals(aCategory.getCreatedAt(), aUpdatedCategory.getCreatedAt())
//                                && aCategory.getUpdatedAt().isBefore(aUpdatedCategory.getUpdatedAt())
//                                && Objects.isNull(aUpdatedCategory.getDeletedAt())
//        ));
    }

    @Test
    public void givenAnInvalidId_whenCallsUpdateCategory_thenShouldReturnNotFoundException(){
        final var expectedId = "123";
        final String expectedName = "Categoria";
        final var expectedDescription = "A categoria mais assistida";
        final var expectedActive = false;

        final var expectedErrorMessage = "The Category with id 123 was not found";

        final var aCommand = UpdateCategoryCommand.with(expectedId, expectedName, expectedDescription,
                expectedActive);

        when(categoryGateway.findById(eq(CategoryID.from("123"))))
                .thenReturn(Optional.empty());

        var domainException = Assertions.assertThrows(NotFoundException.class, () -> useCase.execute(aCommand));

        Assertions.assertNotNull(domainException);
        assertEquals(expectedErrorMessage, domainException.getMessage());

        verify(categoryGateway, times(1)).findById(CategoryID.from("123"));

        verify(categoryGateway, times(0)).update(any());
    }

}
