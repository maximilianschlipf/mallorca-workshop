import { test, expect } from '@playwright/test'

test('catalog is visible and API returns 200', async ({ page, request }) => {
  const apiResponse = await request.get('http://localhost:18081/api/schulungen')
  expect(apiResponse.status()).toBe(200)

  await page.goto('/')
  await expect(page.getByRole('heading', { name: 'Schulungskatalog' })).toBeVisible()
  await expect(page.locator('.card').first()).toBeVisible()
})
