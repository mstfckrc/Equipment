export interface ApiPriceChange {
  itemIndex: number;
  storeId: string;
  listingId: string;
  listingName: string;
  previouslyShownDailyPrice: number;
  previouslyShownLineTotal: number;
  currentDailyPrice: number;
  currentLineTotal: number;
  currencyCode: string;
}

export interface ApiAvailabilityConflict {
  itemIndex: number;
  storeId: string;
  listingId: string;
  date: string;
  requestedQuantity: number;
  remainingQuantity: number;
}

interface ApiErrorPayload {
  code?: string;
  message?: string;
  priceChanges?: ApiPriceChange[];
  availabilityConflicts?: ApiAvailabilityConflict[];
}

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
    public readonly code?: string,
    public readonly priceChanges: ApiPriceChange[] = [],
    public readonly availabilityConflicts: ApiAvailabilityConflict[] = [],
  ) {
    super(message);
    this.name = "ApiError";
  }
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export async function apiRequest<T>(
  path: string,
  init: RequestInit = {},
  accessToken?: string,
): Promise<T> {
  let response: Response;

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      credentials: "include",
      headers: {
        Accept: "application/json",
        ...(init.body ? { "Content-Type": "application/json" } : {}),
        ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
        ...init.headers,
      },
    });
  } catch {
    throw new ApiError(0, "Sunucuya bağlanılamadı.");
  }

  if (!response.ok) {
    const payload = await response.json().catch(() => null) as ApiErrorPayload | null;
    throw new ApiError(
      response.status,
      payload?.message ?? "İstek tamamlanamadı.",
      payload?.code,
      payload?.priceChanges,
      payload?.availabilityConflicts,
    );
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}
