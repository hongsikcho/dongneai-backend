/**
 * 공통 커널. 모든 모듈이 자유롭게 쓰는 기반 타입(BaseTimeEntity, ApiException·ErrorCode, CursorPage, AgeGroup, 코드표)만 둔다.
 * 특정 도메인 개념은 여기에 넣지 않는다.
 */
@ApplicationModule(displayName = "Shared Kernel", type = ApplicationModule.Type.OPEN)
package com.dongneai.shared;

import org.springframework.modulith.ApplicationModule;
