package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name("Should return OK for GET action")
    description("Verifies that the checkOperations endpoint returns OK for a valid GET request")

    request {
        method POST()
        url("/checkOperations")
        headers {
            contentType(applicationJson())
        }
        body([
                action: "GET",
                amount: "500"
        ])
    }

    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body([
                statusCode: "OK",
                statusMessage: "Деньги можно снять"
        ])
    }
}