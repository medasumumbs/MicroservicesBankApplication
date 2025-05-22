package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should find user by username"
    request {
        method GET()
        url("/findByUsername") {
            queryParameters {
                parameter("username", "john_doe")
            }
        }
    }
    response {
        status 200
        body([
                login: "john_doe"
        ])
        headers {
            contentType(applicationJson())
        }
    }
}