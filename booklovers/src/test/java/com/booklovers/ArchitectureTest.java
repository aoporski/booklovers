package com.booklovers;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "com.booklovers")
class ArchitectureTest {

    @ArchTest
    static final ArchRule controllersShouldNotDependOnEntityClasses = 
            noClasses()
                    .that().resideInAPackage("..controller..")
                    .and().haveSimpleNameNotEndingWith("Test")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..entity..")
                    .because("Controllers nie mogą zależeć od klas Entity - powinny używać DTOs (uwaga: enumy mogą być używane w DTO, ale bezpośrednie użycie w controllerze jest naruszeniem)");

    @ArchTest
    static final ArchRule servicesShouldBeInServicePackage = 
            classes()
                    .that().haveSimpleNameEndingWith("Service")
                    .or().haveSimpleNameEndingWith("ServiceImp")
                    .should().resideInAPackage("..service..")
                    .because("Klasy Service muszą być w pakiecie .service");

    @ArchTest
    static final ArchRule repositoriesShouldBeInRepositoryPackage = 
            classes()
                    .that().haveSimpleNameEndingWith("Repository")
                    .should().resideInAPackage("..repository..")
                    .because("Klasy Repository muszą być w pakiecie .repository");

    @ArchTest
    static final ArchRule entitiesShouldBeInEntityPackage = 
            classes()
                    .that().areAnnotatedWith("jakarta.persistence.Entity")
                    .should().resideInAPackage("..entity..")
                    .because("Klasy Entity muszą być w pakiecie .entity");

    @ArchTest
    static final ArchRule controllersShouldNotDependOnRepositories = 
            noClasses()
                    .that().resideInAPackage("..controller..")
                    .and().haveSimpleNameNotEndingWith("Test")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..repository..")
                    .because("Controllers nie mogą zależeć od Repositories - powinny używać Services");

    @ArchTest
    static final ArchRule servicesShouldNotDependOnControllers = 
            noClasses()
                    .that().resideInAPackage("..service..")
                    .should().dependOnClassesThat().resideInAPackage("..controller..")
                    .because("Services nie mogą zależeć od Controllers");

    @ArchTest
    static final ArchRule repositoriesShouldNotDependOnServicesOrControllers = 
            noClasses()
                    .that().resideInAPackage("..repository..")
                    .and().haveSimpleNameNotEndingWith("Test")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..service..", "..controller..")
                    .because("Repositories nie mogą zależeć od Services ani Controllers");

    @ArchTest
    static final ArchRule dtosShouldNotDependOnServicesOrControllers = 
            noClasses()
                    .that().resideInAPackage("..dto..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..service..", "..controller..")
                    .because("DTOs nie mogą zależeć od Services ani Controllers (enumy z Entity są dozwolone)");

    @ArchTest
    static final ArchRule webControllersShouldNotDependOnApiControllers = 
            noClasses()
                    .that().resideInAPackage("..web.controller..")
                    .should().dependOnClassesThat().resideInAPackage("..api.controller..")
                    .because("Web Controllers nie mogą zależeć od API Controllers");


}
