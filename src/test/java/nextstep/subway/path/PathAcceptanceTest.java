package nextstep.subway.path;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.acceptance.LineAcceptanceTest;
import nextstep.subway.line.acceptance.LineSectionAcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.station.StationAcceptanceTest;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("지하철 경로 조회")
public class PathAcceptanceTest extends AcceptanceTest {

    private LineResponse 신분당선;
    private LineResponse 이호선;
    private LineResponse 삼호선;
    private StationResponse 강남역;
    private StationResponse 양재역;
    private StationResponse 교대역;
    private StationResponse 남부터미널역;


    /**
     * 교대역    --- *2호선* ---   강남역
     * |                        |
     * *3호선*                   *신분당선*
     * |                        |
     * 남부터미널역  --- *3호선* ---   양재
     */
    @BeforeEach
    public void setUp() {
        super.setUp();

        강남역 = StationAcceptanceTest.지하철역_등록되어_있음("강남역").as(StationResponse.class);
        양재역 = StationAcceptanceTest.지하철역_등록되어_있음("양재역").as(StationResponse.class);
        교대역 = StationAcceptanceTest.지하철역_등록되어_있음("교대역").as(StationResponse.class);
        남부터미널역 = StationAcceptanceTest.지하철역_등록되어_있음("남부터미널역").as(StationResponse.class);

        신분당선 = LineAcceptanceTest.지하철_노선_등록되어_있음(
                new LineRequest("신분당선", "bg-red-600", 강남역.getId(), 양재역.getId(), 10))
                .as(LineResponse.class);
        이호선 = LineAcceptanceTest.지하철_노선_등록되어_있음(
                new LineRequest("이호선", "bg-red-600", 교대역.getId(), 강남역.getId(), 10))
                .as(LineResponse.class);
        삼호선 = LineAcceptanceTest.지하철_노선_등록되어_있음(
                new LineRequest("삼호선", "bg-red-600", 교대역.getId(), 양재역.getId(), 5))
                .as(LineResponse.class);
        LineSectionAcceptanceTest.지하철_노선에_지하철역_등록_요청(삼호선, 교대역, 남부터미널역, 3);
        삼호선 = LineAcceptanceTest.지하철_노선_조회_요청(삼호선).as(LineResponse.class);
    }

    @Test
    @DisplayName("출발역에서 도착역으로 가는 최단 경로를 구합니다.")
    public void getShortestPath() {
        // given
        // 경로의 출발지 - 도착지
        Long 출발역 = 교대역.getId();
        Long 도착역 = 양재역.getId();

        // when
        ExtractableResponse<Response> response = PathAcceptanceTestSupport.최단경로_조회_요청(출발역, 도착역);

        // then
        PathResponse pathResponse = response.as(PathResponse.class);
        assertThat(pathResponse.getStations().size()).isEqualTo(3);
        assertThat(pathResponse.getDistance()).isEqualTo(5);
    }
}
