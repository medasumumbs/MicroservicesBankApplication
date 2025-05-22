package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should make a transfer between accounts"
    request {
        method POST()
        url "/transfer"
        body([
                fromAccountNumber: "1",
                toAccountNumber  : "2",
                amountFrom       : 100.0,
                amountTo         : 95.0
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
