package io.infinite.david.repositories

import io.infinite.david.entities.Link
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface LinkRepository extends JpaRepository<Link, Long> {

    Set<Link> findByChatId(
            Long chatId
    )

    Set<Link> findByProxyNumber(
            String proxyNumber
    )

    Set<Link> findByAccountNumber(
            String accountNumber
    )

    Set<Link> findByChatIdAndProxyNumber(
            Long chatId,
            String proxyNumber
    )

    Set<Link> findByChatIdAndProxyNumberAndAccountNumber(
            Long chatId,
            String proxyNumber,
            String accountNumber
    )

    Set<Link> findByChatIdAndAccountNumber(
            Long chatId,
            String accountNumber
    )

}
