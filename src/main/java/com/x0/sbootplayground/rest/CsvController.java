package com.x0.sbootplayground.rest;

import com.x0.sbootplayground.data.CsvEntry;
import com.x0.sbootplayground.data.CsvEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.BadRequestException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(value = "/api/csv", produces = MediaType.APPLICATION_JSON_VALUE)
public class CsvController {

    private static final Pattern CELL_CONTENT_REGEX = Pattern.compile("\"(?:|(.+?))\"");
    private static final Pattern QUOTED_CELL_REGEX = Pattern.compile("^\"(.*)\"$");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    @Autowired
    private CsvEntryRepository repo;

    @Transactional()
    @PostMapping
    public void create(@RequestParam("file") MultipartFile file)
            throws RequestParamException
    {
        try (InputStream in = file.getInputStream()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String headersLine = reader.readLine();
                List<BiConsumer<CsvEntry, String>> setters = Arrays.stream(headersLine.split(","))
                        .map(this::getCellContent)
                        .map(this::getPropSetter)
                        .collect(Collectors.toList());

                String line;
                while (null != (line = reader.readLine())) {
                    if (line.trim().isEmpty() || line.equals("\"\"")) {
                        continue;
                    }

                    CsvEntry csv = new CsvEntry();
                    int cellIndex = 0;
                    Matcher m = CELL_CONTENT_REGEX.matcher(line);
                    while (m.find()) {
                        String cellContent = m.group(1);
                        setters.get(cellIndex++).accept(csv, getCellContent(cellContent));
                    }

                    repo.save(csv);
                }
            }
        } catch (IOException e) {
            throw new RequestParamException("file", "is not a valid CSV file");
        }
    }

    @Transactional
    @DeleteMapping
    public void delete()
    {
        repo.deleteAll();
    }

    @GetMapping
    public List<CsvEntry> getAllEntries()
    {
        return StreamSupport.stream(repo.findAll().spliterator(), false).collect(Collectors.toList());
    }

    @GetMapping("{code}")
    public CsvEntry getEntry(@PathVariable String code)
    {
        CsvEntry entry = repo.findByCode(code);
        if (entry == null) {
            throw new BadRequestException(String.format("No entry found with code: \"%s\"", code));
        }
        return entry;
    }


    @ExceptionHandler({ RequestParamException.class })
    private ResponseEntity<Object> handleParamException(RequestParamException ex, WebRequest req) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ CsvFormatException.class })
    private ResponseEntity<Object> handleCsvFormatException(CsvFormatException ex, WebRequest req) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }


    private String getCellContent(String rawContent)
    {
        return rawContent == null ? null : QUOTED_CELL_REGEX.matcher(rawContent).replaceAll("$1");
    }

    private Date parseDate(String val)
    {
        if (val == null || "".equals(val)) {
            return null;
        }
        try {
            return DATE_FORMAT.parse(val);
        } catch (ParseException e) {
            throw new CsvFormatException(String.format("Invalid date format: \"%s\"", val) , e);
        }
    }

    private Integer parseInt(String val)
    {
        if (val == null || "".equals(val)) {
            return null;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            throw new CsvFormatException(String.format("Invalid number format: \"%s\"", val) , e);
        }
    }

    private BiConsumer<CsvEntry, String> getPropSetter(String prop)
    {
        switch (prop) {
            case "source":
                return CsvEntry::setSource;
            case "codeListCode":
                return CsvEntry::setCodeListCode;
            case "code":
                return CsvEntry::setCode;
            case "displayValue":
                return CsvEntry::setDisplayValue;
            case "longDescription":
                return CsvEntry::setLongDescription;
            case "fromDate":
                return (csv, val) -> csv.setFromDate(parseDate(val));
            case "toDate":
                return (csv, val) -> csv.setToDate(parseDate(val));
            case "sortingPriority":
                return (csv, val) -> csv.setSortingPriority(parseInt(val));
            default:
                return (csv, val) -> {
                    // noop
                };
        }
    }

}

class CsvFormatException extends RuntimeException {

    public CsvFormatException(String message, Throwable cause)
    {
        super(message, cause);
    }

}

class RequestParamException extends Exception {

    private final String paramName;
    private final String validationMessage;

    public RequestParamException(String param, String validationMessage) {
        super(String.format("Parameter '%s' %s", param, validationMessage));
        this.paramName = param;
        this.validationMessage = validationMessage;
    }

    public String getParamName() {
        return paramName;
    }

    public String getValidationMessage() {
        return validationMessage;
    }
}
