package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should add a new account for user"
    request {
        method POST()
        url "/addAccount"
        body([
                login       : "john_doe",
                currencyCode: "USD"
        ])
        headers {
            contentType(applicationJson())
        }
    }
    response {
        status 200
        body("OK")
    }
}
