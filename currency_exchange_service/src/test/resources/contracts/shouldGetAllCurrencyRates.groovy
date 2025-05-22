package contracts
import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return all currency exchange rates"
    request {
        method GET()
        url "/rates"
    }
    response {
        status 200
        body([
                [currencyCode: "USD", sellRate: 1.0, buyRate: 1.0],
                [currencyCode: "EUR", sellRate: 0.85, buyRate: 0.85]
        ])
        headers {
            contentType(applicationJson())
        }
    }
}