# Dataset export format

The app can export local Room data as a single time-ordered dataset for table analysis and ML pipelines. Exports are launched from the Reports screen with Android Storage Access Framework, so the user chooses the destination file.

## Formats

- **CSV (`.csv`)**: one header row followed by comma-separated event rows. Values that contain commas, quotes, or line breaks are RFC 4180-style quoted.
- **JSONL (`.jsonl`)**: newline-delimited JSON. Each line is one event object with the same fields as the CSV schema. Empty fields are emitted as `null`.

## Row ordering

Glucose, meal, and insulin records are combined into one stream and sorted by `timestamp_epoch_ms` ascending.

## Schema

| Field | Type | Applies to | Description |
| --- | --- | --- | --- |
| `timestamp_iso` | string | all | UTC ISO-8601 instant generated from the Room timestamp. |
| `timestamp_epoch_ms` | integer | all | Unix epoch timestamp in milliseconds. |
| `event_type` | string | all | One of `glucose`, `meal`, or `insulin`. |
| `glucose_mmol_l` | number/null | glucose | Glucose value in mmol/L. |
| `glucose_mg_dl` | number/null | glucose | Glucose value in mg/dL. |
| `trend_direction` | string/null | glucose | Optional CGM trend direction, when provided by the source. |
| `carbs_g` | number/null | meal | Carbohydrates in grams. |
| `protein_g` | number/null | meal | Protein in grams. |
| `fat_g` | number/null | meal | Fat in grams. |
| `insulin_units` | number/null | insulin | Insulin dose in units. |
| `insulin_type` | string/null | insulin | Insulin category, for example `rapid`, `long`, or `correction`. |
| `source` | string | all | Data source name, for example `Manual`, `Nightscout`, or another configured source. |
| `note` | string/null | meal, insulin | User note for meal or insulin records. Glucose rows leave this empty. |

## CSV example

```csv
timestamp_iso,timestamp_epoch_ms,event_type,glucose_mmol_l,glucose_mg_dl,trend_direction,carbs_g,protein_g,fat_g,insulin_units,insulin_type,source,note
2026-06-15T08:00:00Z,1781510400000,glucose,6.1,109.9,flat,,,,,,Manual,
2026-06-15T08:10:00Z,1781511000000,meal,,,,45.0,20.0,12.0,,,Manual,Breakfast
2026-06-15T08:15:00Z,1781511300000,insulin,,,,,,,4.0,rapid,Manual,Bolus
```

## JSONL example

```jsonl
{"timestamp_iso":"2026-06-15T08:00:00Z","timestamp_epoch_ms":1781510400000,"event_type":"glucose","glucose_mmol_l":6.1,"glucose_mg_dl":109.9,"trend_direction":"flat","carbs_g":null,"protein_g":null,"fat_g":null,"insulin_units":null,"insulin_type":null,"source":"Manual","note":null}
{"timestamp_iso":"2026-06-15T08:10:00Z","timestamp_epoch_ms":1781511000000,"event_type":"meal","glucose_mmol_l":null,"glucose_mg_dl":null,"trend_direction":null,"carbs_g":45.0,"protein_g":20.0,"fat_g":12.0,"insulin_units":null,"insulin_type":null,"source":"Manual","note":"Breakfast"}
```

## Date ranges

The Reports screen offers common ranges: today, last 7 days, last 30 days, and all data. The selected range is applied to each Room query before events are merged.
## ML-ready feature row export

A separate ML-ready export mode can generate uniformly sampled feature rows instead of raw event rows. The generator lives in `com.example.glucosetracker.domain.ml` and accepts glucose, meal, and insulin events for a requested period.

Rows are sampled every 5 minutes. Glucose is carried forward from the most recent reading when it is no more than 20 minutes old; future target glucose values are matched to readings at 30, 60, and 120 minute horizons within half of the 5-minute step.

### Feature schema

| Field | Type | Description |
| --- | --- | --- |
| `timestamp_iso` | string | UTC ISO-8601 row timestamp. |
| `timestamp_epoch_ms` | integer | Unix epoch timestamp in milliseconds. |
| `current_glucose_mmol_l` | number/null | Current glucose in mmol/L. |
| `current_glucose_mg_dl` | number/null | Current glucose in mg/dL. |
| `glucose_delta_15_min_mg_dl` | number/null | Current glucose minus glucose 15 minutes ago. |
| `glucose_delta_30_min_mg_dl` | number/null | Current glucose minus glucose 30 minutes ago. |
| `glucose_delta_60_min_mg_dl` | number/null | Current glucose minus glucose 60 minutes ago. |
| `carbs_last_30_min_g` | number | Carbs logged in the previous 30 minutes. |
| `carbs_last_60_min_g` | number | Carbs logged in the previous 60 minutes. |
| `carbs_last_120_min_g` | number | Carbs logged in the previous 120 minutes. |
| `insulin_last_30_min_units` | number | Insulin units logged in the previous 30 minutes. |
| `insulin_last_60_min_units` | number | Insulin units logged in the previous 60 minutes. |
| `insulin_last_180_min_units` | number | Insulin units logged in the previous 180 minutes. |
| `hour_of_day` | integer | Local hour of day for the row timestamp. |
| `day_of_week` | integer | ISO weekday number, Monday = 1 through Sunday = 7. |
| `target_low_mg_dl` | number | User target range low bound. |
| `target_high_mg_dl` | number | User target range high bound. |
| `target_glucose_30_min_mg_dl` | number/null | Glucose target 30 minutes after the row timestamp. |
| `target_glucose_60_min_mg_dl` | number/null | Glucose target 60 minutes after the row timestamp. |
| `target_glucose_120_min_mg_dl` | number/null | Glucose target 120 minutes after the row timestamp. |
| `target_class_30_min` | string/null | `hypo`, `target`, or `high` at 30 minutes. |
| `target_class_60_min` | string/null | `hypo`, `target`, or `high` at 60 minutes. |
| `target_class_120_min` | string/null | `hypo`, `target`, or `high` at 120 minutes. |