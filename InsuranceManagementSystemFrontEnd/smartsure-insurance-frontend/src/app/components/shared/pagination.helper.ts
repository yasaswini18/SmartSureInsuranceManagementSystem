/**
 * PaginationHelper — embed in any component that needs client-side pagination.
 *
 * Usage:
 *   paginator = new PaginationHelper<MyItem>(10);
 *   paginator.setData(items);          // after API response
 *   paginator.goTo(page);              // on page change
 *   paginator.paged                    // use in *ngFor
 */
export class PaginationHelper<T> {
  private _all: T[] = [];
  currentPage = 1;

  constructor(public pageSize: number = 10) {}

  setData(data: T[]): void {
    this._all = data;
    this.currentPage = 1;
  }

  get total(): number { return this._all.length; }

  get totalPages(): number { return Math.ceil(this._all.length / this.pageSize); }

  get paged(): T[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this._all.slice(start, start + this.pageSize);
  }

  goTo(page: number): void {
    if (page >= 1 && page <= this.totalPages) this.currentPage = page;
  }
}
