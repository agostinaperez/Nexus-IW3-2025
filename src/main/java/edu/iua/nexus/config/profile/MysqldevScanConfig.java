package edu.iua.nexus.config.profile;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
//esto es para los repos
@EnableJpaRepositories(basePackages = "edu.iua.nexus", //le digo q busque mis repos a partir de acá, no los otros q puedan estar afuera
excludeFilters = { //esto es una lista negra de cosas q no quiero q me meta, en este caso lo del cliente 2
}) //esto es para q no escanee los controllers
//para entidades
@EntityScan(basePackages = {"edu.iua.nexus.model",
        "edu.iua.nexus.auth"
         //le digo q busque mis entidades a partir de acá, no las otras q puedan estar afuera
}, basePackageClasses =  {  //puedo pedirle algo extra tipo q me importe alog perticular
    })


@Profile("mysqldev") // esto es estático. Si quisiera algo dinamico uso @ConditionalOnExpression(value = "'${spring.profiles.active:-}'=='cli1'")
public class MysqldevScanConfig {

}