package com.dongneai;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * 모듈 경계 검증 (DB 불필요). 다른 모듈의 내부 패키지(domain·application·infrastructure·presentation)를
 * 참조하거나 모듈 간 순환이 생기면 실패한다. 공개 API는 각 모듈의 api 패키지와 shared(공통 커널)뿐이다.
 */
class ModularityTest {

    private final ApplicationModules modules = ApplicationModules.of(DongneaiApplication.class);

    @Test
    void verifiesModuleBoundaries() {
        modules.verify();
    }
}
