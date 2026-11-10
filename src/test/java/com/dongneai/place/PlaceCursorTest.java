package com.dongneai.place;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dongneai.common.exception.ApiException;
import com.dongneai.place.domain.PlaceCursor;
import com.dongneai.place.domain.PlaceSort;
import org.junit.jupiter.api.Test;

class PlaceCursorTest {

    @Test
    void 정렬별_커서를_해석한다() {
        assertThat(PlaceCursor.parse("41", PlaceSort.LATEST)).isEqualTo(new PlaceCursor(0, 41));
        assertThat(PlaceCursor.parse("23_1", PlaceSort.REVIEWS)).isEqualTo(new PlaceCursor(23, 1));
        assertThat(PlaceCursor.parse(null, PlaceSort.LATEST)).isNull();
    }

    @Test
    void 잘못된_커서는_400() {
        assertThatThrownBy(() -> PlaceCursor.parse("abc", PlaceSort.REVIEWS)).isInstanceOf(ApiException.class);
    }
}
