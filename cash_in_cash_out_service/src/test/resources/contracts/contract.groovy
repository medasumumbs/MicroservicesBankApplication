package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should successfully process a withdrawal"
    request {
        method POST()
        url("/withdrawCash")
        body([
                action     : "GET",
                login      : "john_doe",
                currencyCode: "USD",
                amount     : "100"
        ])
        headers {
            contentType(applicationJson())
        }
    }
    response {
        status 200
        body([
                statusCode   : "OK",
                statusMessage: "Деньги успешно списаны"
        ])
        headers {
            contentType(applicationJson())
        }
    }
}