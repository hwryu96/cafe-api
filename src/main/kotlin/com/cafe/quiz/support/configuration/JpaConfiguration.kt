package com.cafe.quiz.support.configuration

import com.cafe.quiz.support.const.CafeConstant.BASE_PACKAGE
import com.cafe.quiz.support.jpa.annotation.ArchiveRepository
import com.cafe.quiz.support.properties.CustomDataSourceProperties
import jakarta.persistence.EntityManagerFactory
import org.hibernate.cfg.AvailableSettings
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.hibernate5.SpringBeanContainer
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import javax.sql.DataSource

@Configuration
@EnableJpaAuditing
class JpaConfiguration(
    private val properties: CustomDataSourceProperties,
    private val beanFactory: ConfigurableListableBeanFactory,
) {
    @Bean("primaryDataSource")
    @Primary
    fun primaryDataSource() = buildDataSource(properties.getPropertyOrThrow("primary"))

    @Bean("entityManagerFactory")
    @Primary
    fun primaryEntityManagerFactory(
        @Qualifier("primaryDataSource") datasource: DataSource,
    ): LocalContainerEntityManagerFactoryBean =
        LocalContainerEntityManagerFactoryBean().apply {
            dataSource = datasource
            jpaVendorAdapter =
                HibernateJpaVendorAdapter().apply {
                    setShowSql(true)
                    setGenerateDdl(true)
                }

            setJpaPropertyMap(
                mapOf(
                    AvailableSettings.BEAN_CONTAINER to SpringBeanContainer(beanFactory),
                ),
            )

            setPackagesToScan(BASE_PACKAGE)
            persistenceUnitName = "primary"
        }

    @Bean("transactionManager")
    @Primary
    fun primaryTransactionManager(
        @Qualifier("entityManagerFactory") entityManagerFactory: EntityManagerFactory,
    ) = JpaTransactionManager(entityManagerFactory)

    @Bean("archiveDataSource")
    fun archiveDataSource() = buildDataSource(properties.getPropertyOrThrow("archive"))

    @Bean("archiveEntityManagerFactory")
    fun archiveEntityManagerFactory(
        @Qualifier("archiveDataSource") datasource: DataSource,
    ): LocalContainerEntityManagerFactoryBean =
        LocalContainerEntityManagerFactoryBean().apply {
            dataSource = datasource
            jpaVendorAdapter =
                HibernateJpaVendorAdapter().apply {
                    setShowSql(true)
                    setGenerateDdl(true)
                }

            setJpaPropertyMap(
                mapOf(
                    AvailableSettings.BEAN_CONTAINER to SpringBeanContainer(beanFactory),
                ),
            )

            setPackagesToScan(BASE_PACKAGE)
            persistenceUnitName = "archive"
        }

    @Bean("archiveTransactionManager")
    fun archiveTransactionManager(
        @Qualifier("archiveEntityManagerFactory") entityManagerFactory: EntityManagerFactory,
    ) = JpaTransactionManager(entityManagerFactory)

    private fun buildDataSource(property: CustomDataSourceProperties.Properties): DataSource =
        DataSourceBuilder
            .create()
            .url(property.url)
            .username(property.username)
            .password(property.password)
            .driverClassName(property.driverClassName)
            .build()

    @Configuration
    @EnableJpaRepositories(
        basePackages = [BASE_PACKAGE],
        excludeFilters = [
            ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                classes = [ArchiveRepository::class],
            ),
        ],
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager",
    )
    class PrimaryScan

    @Configuration
    @EnableJpaRepositories(
        basePackages = [BASE_PACKAGE],
        includeFilters = [
            ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                classes = [ArchiveRepository::class],
            ),
        ],
        entityManagerFactoryRef = "archiveEntityManagerFactory",
        transactionManagerRef = "archiveTransactionManager",
    )
    class ArchiveScan
}
