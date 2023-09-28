package ee.buerokratt.ruuter.domain.steps.http;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpMethod;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class HttpPutStep extends HttpPostStep {
    @Override
    public String getType() {
        return "http.put";
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.PUT;
    }
}
