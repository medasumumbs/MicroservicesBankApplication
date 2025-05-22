package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should register a new user"
    request {
        method POST()
        url "/register"
        body([
                login   : "john_doe231",
                password: "password123",
                dateOfBirth: "2002-02-02",
                patronymic:"asd",
                firstName:"abc",
                lastName:"def"
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