package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should complete transfer successfully"
    request {
        method POST()
        url("/transfer")
        body([
                fromAccount : "john_doe",
                toAccount   : "jane_doe",
                fromCurrency: "RUB",
                toCurrency  : "RUB",
                amount      : "100"
        ])
        headers {
            contentType(applicationJson())
        }
    }
    response {
        status 200
        body([
                statusCode   : "OK",
                statusMessage: "Перевод успешен"
        ])
        headers {
            contentType(applicationJson())
        }
    }
}