package contracts
import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should save list of currency exchange rates"
    request {
        method POST()
        url "/rates"
        body([
                [currencyCode: "USD", sellRate: 1.0, buyRate: 1.0 ],
                [currencyCode: "EUR", sellRate: 0.85, buyRate: 0.85]
        ])
        headers {
            contentType(applicationJson())
        }
    }
    response {
        status 201
        body([
                statusCode   : "OK",
                statusMessage: "Rates received"
        ])
        headers {
            contentType(applicationJson())
        }
    }
}